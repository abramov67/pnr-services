package com.company.pnrservices.rest;

import com.company.pnrservices.entity.SM160LogInfo;
import com.company.pnrservices.mapper.sm160.Sm160LogInfoMapper;
import com.company.pnrservices.service.SM160LogInfoService;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.core.global.ViewBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sm160")
public class Sm160Controller {
    @Inject
    private SM160LogInfoService sm160LogInfoService;
    @Inject
    private Sm160LogInfoMapper sm160LogInfoMapper;


    @GetMapping(path = "/log-info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> findById(@PathVariable UUID id) {
        System.out.println("!!!run");
        View view = ViewBuilder.of(SM160LogInfo.class).addView(View.LOCAL)
                .build();
        try {
            List<SM160LogInfo> sm160LogInfoList = sm160LogInfoService.find(view);
            return ResponseEntity.ok(sm160LogInfoList.stream().map(it->sm160LogInfoMapper.toResponse(it)).collect(Collectors.toList()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
