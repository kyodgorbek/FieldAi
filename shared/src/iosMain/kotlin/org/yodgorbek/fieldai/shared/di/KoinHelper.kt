package org.yodgorbek.fieldai.shared.di

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.yodgorbek.fieldai.shared.presentation.AssistantViewModel

class KoinHelper : KoinComponent {
    val assistantViewModel: AssistantViewModel by inject()
}
