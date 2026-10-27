package com.tiji.render.pipeline

class PipelineBatch(val pipelines: List<AbstractPipeline>) {
    fun render() {
        pipelines.forEach {
            it.render()
        }
    }
}