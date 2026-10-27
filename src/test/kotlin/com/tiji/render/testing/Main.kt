package com.tiji.render.testing

import com.tiji.render.DefaultLogger
import com.tiji.render.Resource
import com.tiji.render.Window
import com.tiji.render.pipeline.ClearPipeline
import com.tiji.render.pipeline.PipelineBatch

fun main() {
    Window.init (
        "test",
        DefaultLogger,
        4 to 4,

        debug = true,
        clazz = MainPipeline::class.java
    )

    Window.setRenderBatch(PipelineBatch(listOf(
        Resource.markNonResource(ClearPipeline(0.2f, 0.2f, 0.2f, 1f)),
        Window.attach(MainPipeline()),
    )))
    Window.start()
}