package com.gilbeot.gilbut.client.ai;

import com.gilbeot.gilbut.client.ai.dto.scoring.AiRouteScoringRequest;
import com.gilbeot.gilbut.client.ai.dto.scoring.AiRouteScoringResponse;

public interface AiRouteScoringClient {

    AiRouteScoringResponse score(
            AiRouteScoringRequest request
    );
}