package de.fraunhofer.ids.messaging.requests;

import de.fraunhofer.ids.messaging.requests.enums.Crud;
import de.fraunhofer.ids.messaging.requests.enums.Subject;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TemplateResolveService {

    //TODO just first idea, there is probably an more elegant way to resolve this.

    private final NotificationTemplateProvider notificationTemplateProvider;
    private final RequestTemplateProvider requestTemplateProvider;

    public MessageTemplate<?> provideMessageTemplate(Subject subject, Crud operation){
        switch (subject) {
            case OK:
                return notificationTemplateProvider.messageProcessedNotificationMessageTemplate();
            case APP:
                break;
            case LOG:
                break;
            case DATA:
                break;
            case QUERY:
                break;
            case TOKEN:
                break;
            case ARTIFACT:
                break;
            case CONTRACT:
                break;
            case REJECTION:
                break;
            case DESCRIPTION:
                break;
            case PARTICIPANT:
                break;
            case ACCESS_TOKEN:
                break;
            case APP_RESOURCE:
                break;
            case QUERY_RESULT:
                break;
            case CONTRACT_OFFER:
                break;
            case CONTRACT_AGREEMENT:
                break;
            case CONTRACT_REJECTION:
                break;
            case CONTRACT_SUPPLEMENT:
                break;
            default:
                break;
        }
        return null;
    }
}
