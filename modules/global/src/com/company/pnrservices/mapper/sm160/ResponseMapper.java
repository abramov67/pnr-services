package com.company.pnrservices.mapper.sm160;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public interface ResponseMapper<E, R extends Response> extends Serializable {

    R toResponse(@NotNull E entity);
}
