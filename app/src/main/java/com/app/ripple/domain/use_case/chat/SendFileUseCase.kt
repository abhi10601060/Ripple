package com.app.ripple.domain.use_case.chat

import com.app.ripple.data.nearby.model.Message
import com.app.ripple.domain.repo.NearbyShareRepo
import javax.inject.Inject

class SendFileUseCase @Inject constructor(val nearbyShareRepo: NearbyShareRepo) {
    operator fun invoke(metadataMessage: Message) = nearbyShareRepo.sendFile(metadataMessage)
}