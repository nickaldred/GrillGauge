package com.grillgauge.api.domain.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import com.grillgauge.api.domain.entitys.Reading;
import com.grillgauge.api.domain.models.ProbeReading;

public class ProbeReadingToReading implements Converter<ProbeReading, Reading> {

    @NonNull
    public Reading convert(@NonNull final ProbeReading probeReading) {
        return new Reading()
    }
}
