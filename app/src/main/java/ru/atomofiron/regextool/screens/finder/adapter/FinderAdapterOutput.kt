package ru.atomofiron.regextool.screens.finder.adapter

import ru.atomofiron.regextool.screens.finder.adapter.holder.*

interface FinderAdapterOutput :
        FieldHolder.OnActionListener,
        CharactersHolder.OnActionListener,
        ConfigHolder.OnActionListener,
        ProgressHolder.OnActionListener,
        TargetHolder.OnActionListener