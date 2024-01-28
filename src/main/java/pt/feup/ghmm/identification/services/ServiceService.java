package pt.feup.ghmm.identification.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;
import pt.feup.ghmm.identification.enums.ServiceType;
import pt.feup.ghmm.identification.repositories.ServiceRepository;

import java.util.List;
import java.util.Set;

@Service
public class ServiceService {

    private final ServiceRepository repository;

    private final List<String> messagingServices;

    private final List<String> databaseServices;

    private final List<String> logServices;

    public ServiceService(ServiceRepository repository,
                          @Value("${database.services}") String databaseServices,
                          @Value("${messaging.services}") String messagingServices,
                          @Value("${log.services}") String logServices)
    {
        this.repository = repository;
        this.databaseServices = List.of(databaseServices.split(","));
        this.messagingServices = List.of(messagingServices.split(","));
        this.logServices = List.of(logServices.split(","));
    }

    public pt.feup.ghmm.identification.models.Service findOrCreateByName(String name, String image) {
        if(StringUtils.isEmpty(name)){
            name = "undefined";
        }
        if(StringUtils.isEmpty(image)){
            image = "undefined";
        }
        pt.feup.ghmm.identification.models.Service service = repository.findByName(name);
        if(service != null) return service;

        ServiceType serviceType = getServiceType(name, image);
        service = pt.feup.ghmm.identification.models.Service.builder()
                    .name(name)
                    .serviceType(serviceType)
                    .image(image)
                    .build();
        repository.save(service);

        return service;
    }

    public Set<pt.feup.ghmm.identification.models.Service> updateType(Set<pt.feup.ghmm.identification.models.Service> services) {
        for(pt.feup.ghmm.identification.models.Service service: services){
            ServiceType serviceType = getServiceType(service.getName(), service.getImage());
            service.setServiceType(serviceType);
            repository.save(service);
        }
        return services;
    }

    private ServiceType getServiceType(String name, String image) {
        if(keywordListContains(databaseServices, name, image)){
            return ServiceType.DATABASE;
        } else if(keywordListContains(messagingServices, name, image)){
            return ServiceType.MESSAGING;
        }else if(keywordListContains(logServices, name, image)){
            return ServiceType.LOGS;
        } else {
            return ServiceType.GENERIC;
        }
    }

    private boolean keywordListContains(List<String> keywords, String name, String image) {
        for(String keyword: keywords){
            if(name.contains(keyword) || image.contains(keyword)) return true;
        }
        return false;
    }
}
