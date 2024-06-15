import com.kizitonwose.calendar.buildsrc.Plugins

plugins {
    with(com.kizitonwose.calendar.buildsrc.Plugins) {
        applyRootPlugins()
    }
}

allprojects {
    apply(plugin = Plugins.kotlinter)
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.layout.buildDirectory)
}
