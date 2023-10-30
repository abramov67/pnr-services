package com.company.pnrservices.mapper.sm160;

import com.company.pnrservices.dto.sm160.Sm160LogInfoResponse;
import com.company.pnrservices.entity.SM160LogInfo;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@Component(Sm160LogInfoMapper.NAME)
public class Sm160LogInfoMapper implements ResponseMapper<SM160LogInfo, Sm160LogInfoResponse> {
    public static final String NAME = "Sm160LogInfoMapper";
    private static final long serialVersionUID = -8852741407307699895L;

    @Override
    public Sm160LogInfoResponse toResponse(@NotNull SM160LogInfo entity) {
        Sm160LogInfoResponse sm160LogInfoResponse = new Sm160LogInfoResponse();
        return sm160LogInfoResponse;
    }
}
