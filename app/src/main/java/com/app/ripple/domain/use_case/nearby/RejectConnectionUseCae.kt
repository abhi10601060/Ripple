package com.app.ripple.domain.use_case.nearby

import com.app.ripple.domain.repo.NearbyShareRepo
import javax.inject.Inject

class RejectConnectionUseCae @Inject constructor(
    val nearbyShareRepo: NearbyShareRepo
) {
    operator fun invoke(endpointId: String) =  nearbyShareRepo.rejectConnection(endpointId = endpointId)
}