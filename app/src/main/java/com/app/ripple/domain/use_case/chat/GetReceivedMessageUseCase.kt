package com.app.ripple.domain.use_case.chat

import com.app.ripple.domain.repo.NearbyShareRepo
import javax.inject.Inject

class GetReceivedMessageUseCase @Inject constructor(val nearbyShareRepo: NearbyShareRepo) {
    operator fun invoke() = nearbyShareRepo.getReceivedMessages()
}