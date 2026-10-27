package com.tiji.render

/**
 * Represents a resource that **must be destroyed** before application exit
 */
abstract class Resource {
    companion object {
        val unfreedResources = mutableListOf<Resource>()

        fun logUnfreedResources() {
            if (unfreedResources.isNotEmpty()) {
                Window.logger.error("There are ${unfreedResources.size} unfreed resources! These are follows:")
                unfreedResources.forEach {
                    Window.logger.error(it.toString())
                }
            }
        }

        /**
         * Marks that this resource is actually not resource
         * (as in not needing to be freed)
         */
        fun <T: Resource>markNonResource(resource: T): T {
            if (Window.debug) {
                unfreedResources.remove(resource)
            }
            return resource
        }
    }

    init {
        if (this::class.simpleName != "Window" && Window.debug) {
            unfreedResources += this
        }
    }

    val attachedResource: MutableList<Resource> = mutableListOf()

    /**
     * Attaches different resource to this resource,
     * tying that resource's lifetime to this one.
     */
    fun <T: Resource>attach(resource: T): T {
        attachedResource.add(resource)
        return resource
    }

    private var _isDestroyed = false
    val isDestroyed: Boolean get() = _isDestroyed
    /**
     * Destroys the resource
     */
    open fun destroy() {
        require(!isDestroyed) { "Already destroyed" }

        attachedResource.forEach {
            it.destroy()
        }

        unfreedResources.remove(this)
        _isDestroyed = true
    }
}