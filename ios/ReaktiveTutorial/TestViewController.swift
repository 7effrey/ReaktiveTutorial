//
//  TestViewController.swift
//  ReaktiveTutorial
//
//  Created by Jeffrey Hermanto Halimsetiawan on 13/11/2020.
//  Copyright © 2020 Jeffrey Hermanto Halimsetiawan. All rights reserved.
//

import UIKit
import Core

class TestViewController: UIViewController {
    private var _movies: [Movie]?
    private var _isRefreshing = false
    
    lazy var refreshControl: UIRefreshControl = {
        let v = UIRefreshControl()
        
        v.addTarget(self, action: #selector(refresh), for: .valueChanged)
        
        return v
    }()
    
    lazy var collectionView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        
        layout.minimumLineSpacing = 20
        layout.scrollDirection = .vertical
        layout.sectionInset = UIEdgeInsets(top: 20, left: 10, bottom: 5, right: 10)
        
        let marginsAndInsets = layout.sectionInset.left + layout.sectionInset.right + layout.minimumInteritemSpacing * CGFloat(2 - 1)
        let itemWidth = ((UIScreen.main.bounds.size.width - marginsAndInsets) / CGFloat(2)).rounded(.down)
        
        let itemSize = CGSize(width: itemWidth, height: 250)
        
        layout.itemSize = itemSize
        
        let v = UICollectionView(frame: .zero, collectionViewLayout: layout)
        
        v.backgroundColor = UIColor(named: "ListBackground")
        v.delegate = self
        v.dataSource = self
        v.alwaysBounceVertical = true
        v.refreshControl = refreshControl
        v.translatesAutoresizingMaskIntoConstraints = false
        v.register(MovieCell.self, forCellWithReuseIdentifier: "MovieCell")
        
        return v
    }()
    
    private lazy var _viewModel: TestViewModel = {
//        let delegate = UIApplication.shared.delegate as! AppDelegate
//        let cache = MovieSqlCache(db: delegate.dbHelper)
        
        let httpClient: HttpClient = KtorHttpClient(baseUrl: "https://www.omdbapi.com/", logging: true)
        let provider: OmdbProvider = DefaultOmdbProvider(httpClient: httpClient, apiKey: "b445ca0b")
        let storageService: StorageService = DefaultStorageService()
        let repository: MovieRepository = DefaultMovieRepository(omdbProvider: provider, storageService: storageService)

        let viewModel = DefaultTestViewModel<Movie>(movieRepository: repository, mapper: nil)
        
        return viewModel
    }()
    
    private var disposables: CompositeDisposableWrapper?
    
    private var query = "secret life"
    
    deinit {
        disposables?.dispose()
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        binding()
        
        let button = UIButton()
        button.translatesAutoresizingMaskIntoConstraints = false
        button.backgroundColor = .gray
        button.setTitle("NEXT", for: .normal)
        button.setTitleColor(.white, for: .normal)
        button.titleLabel?.font = UIFont.boldSystemFont(ofSize: 12.0)
        button.titleLabel?.textAlignment = .center//Text alighment center
        button.titleLabel?.numberOfLines = 0//To display multiple lines in UIButton
        button.titleLabel?.lineBreakMode = .byWordWrapping//By word wrapping
        button.addTarget(self, action:#selector(self.buttonClicked), for: .touchUpInside)
        view.addSubview(button)
        
        view.addSubview(collectionView)
        
        NSLayoutConstraint.activate([
            button.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 16),
            button.topAnchor.constraint(equalTo: view.topAnchor, constant: 88 + 16),
            button.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -16),
            button.heightAnchor.constraint(equalToConstant: 60),
            
            collectionView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            collectionView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            collectionView.topAnchor.constraint(equalTo: button.bottomAnchor),
            collectionView.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        ])
        
        get()
    }
    
    @objc func buttonClicked() {
        _viewModel.viewEvent.pressNext()
    }
    
    // MARK - Selector
    @objc func refresh() {
        get()
    }
    
    private func get() {
        _viewModel.viewEvent.get(query: query)
    }
    
    private func loadMore() {
        _viewModel.viewEvent.loadMore(query: query)
    }
    
    // MARK - Private
    
    private func binding() {
        
        disposables = CompositeDisposableWrapper()

        disposables?.add(disposable: _viewModel.set())

        let viewData: TestViewModelViewData = _viewModel.generateViewData()

        disposables?.add(disposable: viewData.loadingW.subscribe(isThreadLocal: true) { [weak self] result in
            guard let strongSelf = self, let loading = result as? Bool else { return }

            strongSelf._isRefreshing = loading

            if loading {
                strongSelf.refreshControl.beginRefreshing()
            } else {
                strongSelf.refreshControl.endRefreshing()
            }
        })

        disposables?.add(disposable: viewData.resultW.subscribe(isThreadLocal: true) { [weak self] result in
            guard let strongSelf = self, let list = result as? [Movie] else { return }

            strongSelf._movies = list
            strongSelf.collectionView.reloadData()
        })
        
        disposables?.add(disposable: _viewModel.outputW.subscribe(isThreadLocal: true) { [weak self] result in
            guard let strongSelf = self else { return }
            
            let alert = UIAlertController(title: "Press Next", message: "Navigate to Detail", preferredStyle: UIAlertController.Style.alert)
            alert.addAction(UIAlertAction(title: "OK", style: UIAlertAction.Style.default, handler: nil))
            strongSelf.present(alert, animated: true, completion: nil)
        })
    }
}

extension TestViewController: UICollectionViewDataSource, UICollectionViewDelegate {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return _movies?.count ?? 0
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "MovieCell", for: indexPath) as! MovieCell
        
        cell.movie = _movies?[indexPath.row]
        
        return cell
    }
    
    func scrollViewDidScroll(_ scrollView: UIScrollView) {
        let bottomEdge = scrollView.contentOffset.y + scrollView.frame.size.height;
        
        if (bottomEdge + 200 >= scrollView.contentSize.height && scrollView.contentOffset.y > 0 && !_isRefreshing) {
            loadMore()
        }
    }
}
