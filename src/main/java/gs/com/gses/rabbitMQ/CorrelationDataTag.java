package gs.com.gses.rabbitMQ;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;



@Data
@NoArgsConstructor
public class CorrelationDataTag extends CorrelationData {

    private Message message;

    public CorrelationDataTag(String id, Message message) {
        super(id);
        this.message = message;
    }


}