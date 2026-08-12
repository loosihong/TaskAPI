package com.example.TaskAPI.core.audit;

import java.util.Map;

public interface Snapshotable {
    Map<String, Object> getSnapshot();
}
