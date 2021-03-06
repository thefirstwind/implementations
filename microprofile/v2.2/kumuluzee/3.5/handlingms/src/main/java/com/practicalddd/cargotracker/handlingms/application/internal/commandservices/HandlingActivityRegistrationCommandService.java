package com.practicalddd.cargotracker.handlingms.application.internal.commandservices;

import com.practicalddd.cargotracker.handlingms.domain.model.aggregates.HandlingActivity;
import com.practicalddd.cargotracker.handlingms.domain.model.commands.HandlingActivityRegistrationCommand;
import com.practicalddd.cargotracker.handlingms.domain.model.valueobjects.CargoBookingId;
import com.practicalddd.cargotracker.handlingms.domain.model.valueobjects.Location;
import com.practicalddd.cargotracker.handlingms.domain.model.valueobjects.Type;
import com.practicalddd.cargotracker.handlingms.domain.model.valueobjects.VoyageNumber;
import com.practicalddd.cargotracker.handlingms.infrastructure.repositories.jpa.HandlingActivityRepository;
import com.practicalddd.cargotracker.shareddomain.events.CargoHandledEventData;
import com.practicalddd.cargotracker.shareddomain.events.CargoHandledEvent;

import com.practicalddd.cargotracker.handlingms.infrastructure.brokers.producers.HandlingMessageProducer;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class HandlingActivityRegistrationCommandService {

        @Inject
        private HandlingActivityRepository handlingActivityRepository;

        @Inject
        private HandlingMessageProducer handlingMessageProducer;

        /**
         * Service Command method to register a new Handling Activity
         * @return BookingId of the CargoBookingId
         */
        @Transactional
        public void registerHandlingActivityService(HandlingActivityRegistrationCommand handlingActivityRegistrationCommand){
                System.out.println("***Handling Voyage Number***");
                if(!handlingActivityRegistrationCommand.getVoyageNumber().equals("")) {
                        HandlingActivity handlingActivity = new HandlingActivity(
                                new CargoBookingId(handlingActivityRegistrationCommand.getBookingId()),
                                handlingActivityRegistrationCommand.getCompletionTime(),
                                Type.valueOf(handlingActivityRegistrationCommand.getHandlingType()),
                                new Location(handlingActivityRegistrationCommand.getUnLocode()),
                                new VoyageNumber(handlingActivityRegistrationCommand.getVoyageNumber()));
                        handlingActivityRepository.store(handlingActivity);


                }else{
                        HandlingActivity handlingActivity = new HandlingActivity(
                                new CargoBookingId(handlingActivityRegistrationCommand.getBookingId()),
                                handlingActivityRegistrationCommand.getCompletionTime(),
                                Type.valueOf(handlingActivityRegistrationCommand.getHandlingType()),
                                new Location(handlingActivityRegistrationCommand.getUnLocode()));
                        handlingActivityRepository.store(handlingActivity);
                }


                CargoHandledEventData eventData = new CargoHandledEventData();
                eventData.setBookingId(handlingActivityRegistrationCommand.getBookingId());
                eventData.setHandlingCompletionTime(handlingActivityRegistrationCommand.getCompletionTime());
                eventData.setHandlingLocation(handlingActivityRegistrationCommand.getUnLocode());
                eventData.setHandlingType(handlingActivityRegistrationCommand.getHandlingType());
                eventData.setVoyageNumber(handlingActivityRegistrationCommand.getVoyageNumber());

                handlingMessageProducer.sendCargoHandledEvent(eventData);

        }
}
