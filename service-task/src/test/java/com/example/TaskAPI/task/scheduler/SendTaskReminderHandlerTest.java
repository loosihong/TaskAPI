package com.example.TaskAPI.task.scheduler;

import com.example.TaskAPI.task.api.dto.TaskReminderResult;
import com.example.TaskAPI.task.service.TaskReminderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SendTaskReminderHandlerTest {
    @Mock
    private TaskReminderService taskReminderService;
    @InjectMocks
    private SendTaskReminderHandler sendTaskReminderHandler;

    @Test
    void delegatesToTaskReminderService() {
        UUID taskUuid = UUID.randomUUID();

        when(taskReminderService.sendReminder(taskUuid))
                .thenReturn(TaskReminderResult.SENT);

        sendTaskReminderHandler.run(new SendTaskReminder(taskUuid));

        verify(taskReminderService).sendReminder(taskUuid);
    }
}
