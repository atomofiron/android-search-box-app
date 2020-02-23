package ru.atomofiron.regextool.screens.finder.model

class FinderState(
        val replaceEnabled: Boolean,
        val characters: Array<String>,
        val configVisible: Boolean,
        val progress: List<FinderProgress>,
        val result: List<FinderResult>
) {
    fun copy(replaceEnabled: Boolean = this.replaceEnabled,
             characters: Array<String> = this.characters,
             configVisible: Boolean = this.configVisible,
             progress: List<FinderProgress> = this.progress,
             result: List<FinderResult> = this.result): FinderState {
        return FinderState(replaceEnabled, characters, configVisible, progress, result)
    }
}