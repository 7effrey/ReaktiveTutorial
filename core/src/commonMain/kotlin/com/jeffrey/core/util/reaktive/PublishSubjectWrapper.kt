package com.jeffrey.core.util.reaktive

import com.badoo.reaktive.subject.publish.PublishSubject

open class PublishSubjectWrapper<T : Any>(inner: PublishSubject<T>) : PublishSubject<T> by inner {

    constructor() : this(PublishSubject<T>())
}

fun <T : Any> PublishSubject<T>.wrap(): PublishSubjectWrapper<T> = PublishSubjectWrapper(this)