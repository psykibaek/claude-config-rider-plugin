package com.gear2w.claudeconfig

import java.io.File

/**
 * 트리에 표시되는 한 노드.
 *
 * @param label  화면에 보일 이름
 * @param file   실제 파일(파일 노드인 경우). 그룹/폴더 노드면 null
 */
class ConfigNode(
    val label: String,
    val file: File? = null,
) {
    val isFile: Boolean get() = file != null
    override fun toString(): String = label
}
