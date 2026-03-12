// com/example/educationloan/report/AuthLogStore.java
package com.example.educationloan.report;

import com.example.educationloan.dto.AuthLogDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class AuthLogStore {

    private final List<AuthLogDTO> logs =
            Collections.synchronizedList(new ArrayList<>());

    public void add(AuthLogDTO log) {
        logs.add(log);
    }

    public List<AuthLogDTO> getAll() {
        return logs;
    }
}