package com.ashenone.api.ui;

import com.ashenone.api.proto.BaeldungProto;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.TextFormat;
import com.google.protobuf.util.JsonFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/protobuf")
public class ProtobufController {

    @PostMapping(value = "/receive", consumes = "application/x-protobuf", produces = "application/x-protobuf")
    public ResponseEntity send(@RequestBody BaeldungProto.Course course) throws InvalidProtocolBufferException {
        log.info("Course received: {}", TextFormat.printer().printToString(course));
        return ResponseEntity.ok(JsonFormat.printer().print(course));
    }
}
