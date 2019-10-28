package com.tufin.webhook.exceptions


class PersistenceException : Exception {

    constructor(message: String) : super(message) {}

    constructor(message: String, cause: Throwable) : super(message, cause) {}
}
