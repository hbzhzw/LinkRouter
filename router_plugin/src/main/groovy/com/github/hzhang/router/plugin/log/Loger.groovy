package com.github.hzhang.router.plugin.log

import org.gradle.api.Project

class Loger {
    public static Project PROJ

    static void v(String tag, String msg) {
        PROJ.logger.debug("${tag}: ${msg}")
    }

    static void d(String tag, String msg) {
        PROJ.logger.info("${tag}: ${msg}")
    }

    static void i(String tag, String msg) {
        PROJ.logger.lifecycle("${tag}: ${msg}")
    }

    static void e(String tag, String msg) {
        PROJ.logger.error("ERROR: ${tag}: ${msg}")
    }

    static void w(String tag, String msg) {
        PROJ.logger.error("WARN: ${tag}: ${msg}")
    }
}