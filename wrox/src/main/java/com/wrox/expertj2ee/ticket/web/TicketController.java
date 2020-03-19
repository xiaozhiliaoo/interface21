package com.wrox.expertj2ee.ticket.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java14.java.util.logging.Level;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.TopicSession;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.interface21.beans.ClassLoaderAnalyzer;
import com.interface21.context.ApplicationContextException;
import com.interface21.jms.JmsTemplate;
import com.interface21.validation.BindException;
import com.interface21.validation.Validator;
import com.interface21.web.bind.HttpServletRequestDataBinder;
import com.interface21.web.bind.RequestUtils;
import com.interface21.web.bind.WebRequestBindingException;
import com.interface21.web.servlet.ModelAndView;
import com.interface21.web.servlet.mvc.multiaction.MultiActionController;
import com.wrox.expertj2ee.ticket.boxoffice.*;
import com.wrox.expertj2ee.ticket.command.AvailabilityCheck;
import com.wrox.expertj2ee.ticket.command.PerformanceWithAvailability;
import com.wrox.expertj2ee.ticket.command.PriceBandWithAvailability;
import com.wrox.expertj2ee.ticket.customer.RegisteredUser;
import com.wrox.expertj2ee.ticket.exceptions.NoSuchPerformanceException;
import com.wrox.expertj2ee.ticket.referencedata.Calendar;
import com.wrox.expertj2ee.ticket.referencedata.Performance;
import com.wrox.expertj2ee.ticket.referencedata.Show;
import com.wrox.expertj2ee.ticket.web.model.BookingFormModel;

/**
 * This class contains all web tier controller code for the Ticketing Application.
 * <p>
 * By extending MultiActionController we get method-based, rather
 * than class-based mapping of requests to handling code.
 *
 * @author Rod Johnson
 */
public class TicketController extends MultiActionController implements WebConstants {


    //---------------------------------------------------------------------
    // Instance data
    //---------------------------------------------------------------------
    private BoxOffice boxOffice;

    private Calendar calendar;

    private AvailabilityCheck availabilityCheck;

    private int minutesToHold;

    private double bookingFee;

    private Validator validator;

    //---------------------------------------------------------------------
    // Bean properties
    //---------------------------------------------------------------------

    /**
     * Sets the boxOffice.
     *
     * @param boxOffice The boxOffice to set
     */
    public void setBoxOffice(BoxOffice boxOffice) {
        logger.info("Setting boxOffice to [" + boxOffice + "]");
        this.boxOffice = boxOffice;
    }

    /**
     * Sets the calendar.
     *
     * @param calendar The calendar to set
     */
    public void setCalendar(Calendar calendar) {


        this.calendar = calendar;
    }

    public void setAvailabilityCheck(AvailabilityCheck availabilityCheck) {
        this.availabilityCheck = availabilityCheck;
    }

    /**
     * Moves logic out
     */
    public void setUserValidator(Validator validator) throws IllegalArgumentException {
        if (!validator.supports(RegisteredUser.class))
            throw new IllegalArgumentException("Validator can't validate RegisteredUser class");
        this.validator = validator;
    }

    public void setBookingFee(double bookingFee) {
        this.bookingFee = bookingFee;
    }

    public void setMinutesToHoldReservations(int minutesToHold) {
        this.minutesToHold = minutesToHold;
    }

    /**
     * @see AbstractController#init()
     */
    protected final void init() throws ApplicationContextException {
        if (boxOffice == null)
            throw new ApplicationContextException("Must set boxOffice bean property on " + getClass());
        if (calendar == null)
            throw new ApplicationContextException("Must set calendar bean property on " + getClass());
        if (availabilityCheck == null)
            throw new ApplicationContextException("Must set availabilityCheck bean property on " + getClass());
        if (this.validator == null)
            throw new ApplicationContextException("userValidator property must be set on " + getClass());
    }

    //---------------------------------------------------------------------
    // Request handling actions
    //---------------------------------------------------------------------
    public ModelAndView displayGenresPage(HttpServletRequest request, HttpServletResponse response) {
        List genres = calendar.getCurrentGenres();

        cacheForSeconds(response, 300, true);
        return new ModelAndView("welcomeView", "genres", genres);
    }

    /**
     * Last modified handler for the genres page
     */
    public long displayGenresPageLastModified(HttpServletRequest request) {
        return calendar.getLastModified();
    }

    /**
     * We don't need to catch NoSuchPerformanceException: it shouldn't happen
     * Single parameter: id
     */
    public ModelAndView displayShow(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, NoSuchPerformanceException {

        String st = ClassLoaderAnalyzer.showClassLoaderHierarchy(this, "ticketcontroller", "\n", "\t");
        System.out.println(st);
        logger.warning(st);

        // TODO: should really accept get and use id in URL
        // Mappings would need to be more sophisticated...
        //RequestUtils.rejectRequestMethod(request, "GET");

        String showId = request.getParameter("id");
        if (showId == null)
            return new ModelAndView("invalidRequestView");
        int id = Integer.parseInt(showId);

        Show s = calendar.getShow(id);
        s = availabilityCheck.getShowWithAvailability(s, true);

        cacheForSeconds(response, 30, true);
        return new ModelAndView("displayShowView", "show", s);
    }

    // HOW TO DO SHOW!?

    /**
     * Expects params: id: show
     * type: seat id in show
     * We can assume that these won't be invalid
     * NEED A BASE COMMAND HERE!???? COULD THEN USE BIND FORM OF THE METHOD
     * CAN DISCOUNT INVALID INPUT
     */
    public ModelAndView displayBookSeatsForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, NoSuchPerformanceException {


        //rejectRequestMethod(request, "GET");

        // Throws a ServletException on failure
        int performanceId = RequestUtils.getRequiredIntParameter(request, "id");
        int type = RequestUtils.getRequiredIntParameter(request, "type");

        // Do with bind!?

        Performance p = calendar.getPerformance(performanceId);
        PerformanceWithAvailability pa = availabilityCheck.getPerformanceWithAvailability(p, false);
        PriceBandWithAvailability selectedPriceBand = null;
        List priceBands = pa.getPriceBands();
        for (int i = 0; i < priceBands.size(); i++) {
            PriceBandWithAvailability pb = (PriceBandWithAvailability) priceBands.get(i);
            if (pb.getSeatTypeId() == type) {
                selectedPriceBand = pb;
            }
        }
        if (selectedPriceBand == null)
            throw new ServletException("Invalid seat type " + type + " for performance with id=" + performanceId);

        //		if (NO AVAILABILITY

        // Apply rule
        int maxDropDownValue = selectedPriceBand.getFreeSeats();

        // Separate model objects? not one?

        BookingFormModel bean = new BookingFormModel(pa, selectedPriceBand, maxDropDownValue);

        preventCaching(response);
        return new ModelAndView("selectSeatsForm", "bookingData", bean);
    }


    public ModelAndView processSeatSelectionFormSubmission(
            HttpServletRequest request,
            HttpServletResponse response,
            ReservationRequest reservationRequest)
            throws ServletException, InvalidSeatingRequestException, NoSuchPerformanceException {

        logger.fine("Reservation request after bind is [" + reservationRequest + "]");

        // Apply standard policies
        reservationRequest.setBookingFee(this.bookingFee);
        reservationRequest.setReserve(true);
        reservationRequest.holdFor(this.minutesToHold);
        reservationRequest.setSeatsMustBeAdjacent(true);
        logger.fine("Reservation request after applying policies is [" + reservationRequest + "]");

        // Find the performance
        Performance performance = this.calendar.getPerformance(reservationRequest.getPerformanceID());

        // Model to be displayed to view. We must always include the performance.
        Map model = new HashMap();
        model.put(PERFORMANCE_KEY, performance);
        // Make things really easy for the view...
        // It cannot encounter _any_ errors while rendering, by supplying an invalid seat type id
        model.put(PRICE_BAND_KEY, performance.getPriceBandForSeatType(reservationRequest.getClassID()));

        Reservation reservation = null;
        HttpSession session = request.getSession(false);
        if (session != null) {
            reservation = (Reservation) session.getAttribute(RESERVATION_KEY);
            if (reservation != null && !reservation.satisfiesRequest(reservationRequest)) {
                // Old reservation that doesn't match was found in session. Clear it.
                logger.info("Clearing old reservation that doesn't match new request");
                reservation = null;
                session.removeAttribute(RESERVATION_KEY);
            }
        }

        try {
            // Reservation may still be null if not found in session
            // In this case we need to try to create one
            if (reservation == null) {
                reservation = boxOffice.allocateSeats(reservationRequest);
            }

            // Create a user session if none exists
            session = request.getSession(true);
            logger.fine("Added reservation to user session");
            session.setAttribute(RESERVATION_KEY, reservation);
            model.put(RESERVATION_KEY, reservation);
            return new ModelAndView(SHOW_RESERVATION_VIEW, model);
        } catch (NotEnoughSeatsException ex) {
            logger.info("Not enough seats: " + ex);
            model.put(NOT_ENOUGH_SEATS_KEY, ex);
            return new ModelAndView("notEnoughSeats", model);
        }
    }    // processSeatSelectionFormSubmission


    //--------------------------------------------- TEST vvvv
    private Reservation oldReservation;

    /**
     * The framework will perform data binding before invoking this.
     * We know that there were no errors.
     */
    public ModelAndView processSeatSelectionFormSubmissionTEST(
            HttpServletRequest request,
            HttpServletResponse response,
            ReservationRequest reservationRequest)
            throws ServletException, InvalidSeatingRequestException, NoSuchPerformanceException {

        logger.fine("Reservation request after bind is [" + reservationRequest + "]");

        // Apply standard policies
        reservationRequest.setBookingFee(this.bookingFee);
        reservationRequest.setReserve(true);
        reservationRequest.holdFor(this.minutesToHold);
        reservationRequest.setSeatsMustBeAdjacent(true);
        if (logger.isLoggable(Level.FINE))
            logger.fine("Reservation request after applying policies is [" + reservationRequest + "]");

        // Find the performance
        Performance performance = this.calendar.getPerformance(reservationRequest.getPerformanceID());

        // Model to be displayed to view. We must always include the performance.
        Map model = new HashMap();
        model.put(PERFORMANCE_KEY, performance);
        // Make things really easy for the view...
        // It cannot encounter _any_ errors while rendering, by supplying an invalid seat type id
        model.put(PRICE_BAND_KEY, performance.getPriceBandForSeatType(reservationRequest.getClassID()));

        Reservation reservation = oldReservation;

        try {
            // Reservation may still be null if not found in session
            // In this case we need to try to create one
            if (reservation == null) {
                reservation = boxOffice.allocateSeats(reservationRequest);
                this.oldReservation = reservation;
                logger.severe("Made new reservation " + reservation);
            }

            model.put(RESERVATION_KEY, reservation);

            String vname = SHOW_RESERVATION_VIEW;
            if (request.getServletPath().indexOf(".pdf") != -1)
                vname = "pdf_" + vname;
            else {
                String view = request.getParameter("view");
                if (view != null)
                    vname = view + "_" + vname;
            }

            return new ModelAndView(vname, model);
        } catch (NotEnoughSeatsException ex) {
            logger.info("Not enough seats: " + ex);
            model.put(NOT_ENOUGH_SEATS_KEY, ex);
            return new ModelAndView("notEnoughSeats", model);
        }
    }    // processSeatSelectionFormSubmissionTEST

    //--------------------------------------------- TEST ^^^^


    /**
     * Display payment form
     * Depends on a session
     * No parameters
     */
    public ModelAndView displayPaymentForm(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ServletException, InvalidSeatingRequestException, NoSuchPerformanceException {

        // We must simply check that there's a reservation in the user's session.
        // We may have a purchase request if this is a refresh
        Reservation reservation = (Reservation) session.getAttribute(RESERVATION_KEY);
        if (reservation == null)
            throw new ServletException("Illegal workflow: Reservation was required in session to access payment form");

        // Get user details from session if possible or lookup?
        RegisteredUser user = (RegisteredUser) session.getAttribute(USER_KEY);
        if (user == null) {
            // TRY TO LOOK UP USER!?
            user = new RegisteredUser();
            user.setEmail("Joe Bloggs");
            session.setAttribute(USER_KEY, user);
        }

        HashMap m = new HashMap();
        m.put(USER_KEY, user);
        return new ModelAndView("paymentForm", m);
    }


    /**
     * Uses manual binding
     */
    public ModelAndView processPaymentFormSubmission(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session)
            throws ServletException, BoxOfficeInternalException, InvalidSeatingRequestException {

        // We must simply check that there's a reservation in the user's session.
        // COULD ALSO MODEL AS A SUPERCLASS CONTROLLER
        Reservation reservation = (Reservation) session.getAttribute(RESERVATION_KEY);
        RegisteredUser user = (RegisteredUser) session.getAttribute(USER_KEY);
        if (reservation == null || user == null)
            throw new ServletException("Illegal workflow: Reservation and User are required in session to submit payment form");

        HttpServletRequestDataBinder binder = null;
        try {
            binder = new HttpServletRequestDataBinder(user, USER_KEY);
            binder.bind(request);
            validator.validate(user, binder);

            PurchaseRequest purchaseRequest = new PurchaseRequest(reservation, user);
            binder.newTarget(purchaseRequest, "purchase");
            binder.bind(request);

            // REMOVE VALIDATION INTO SEPARATE CLASS
            // We add errors on fields that aren't there
            if (purchaseRequest.getCardNumber() == null || purchaseRequest.getCardNumber().length() != 16)
                binder.rejectValue("cardNumber", "cardnumber.wronglength", "Wrong length");
            try {
                Long.parseLong(purchaseRequest.getCardNumber());
            } catch (NumberFormatException ex) {
                binder.rejectValue("cardNumber", "cardNumber", "Credit card number is non-numeric");
            }
            if (purchaseRequest.getExpiry() == null || purchaseRequest.getExpiry().length() != 4)
                binder.rejectValue("expiry", "expiry.wronglength", "Wrong expiry length");

            // May throw exception
            binder.close();

            // Do payment with user
            logger.fine("Have valid user details: will use them to book");

            boxOffice.confirmReservation(purchaseRequest);
            session.removeAttribute("reservation");

            return new ModelAndView("debugView", binder.getModel());
        } catch (BindException ex) {
            // Bind failure
            return new ModelAndView("paymentForm", binder.getModel());
        } catch (CreditCardAuthorizationException ex) {
            // Bind failure
            // already added to request: just choose different view
            //			sesssion.removeA
            return new ModelAndView(DEBUG_VIEW, "INVALID CCN", ex);
        } catch (ExpiredReservationTakenException ex) {
            // Bind failure
            // already added to request: just choose different view
            return new ModelAndView(DEBUG_VIEW, "Expired reservation taken!!!!", ex);
        }
    }


    /**
     * Move JMS into a helper?  But initiating this is a GUI responsibility.
     * WHEN A RESERVATION IS MADE - ELSEWHERE - AN UPDATE EVENT MUST BE SET TO THAT PERFORMANCE,
     * BUT MUST ALSO SOMEHOW SCHEDULE THE SENDING OF ANOTHER FOR THE EXPIRY DATE OF THAT
     * RESERVATION, in case the reservation expires
     * IS THIS TOO COMPLICATED?
     */
    public ModelAndView refreshReferenceData(HttpServletRequest request, HttpServletResponse response) {
        // CHECK USER's ROLE...
        JmsTemplate jmsTemplate = new JmsTemplate("jms/TopicFactory");

        // CONSTANT
        jmsTemplate.publish("jms/topic/data-update", new JmsTemplate.PubSubMessageCreator() {
            public Message createMessage(TopicSession session) throws JMSException {
                TextMessage msg = session.createTextMessage();
                msg.setStringProperty("affects", "referenceData");
                msg.setText("This is a test message");
                return msg;
            }
        });
        return new ModelAndView(DATA_REFRESH_VIEW);
    }

    //---------------------------------------------------------------------
    // Exception handlers: stop the MultiAction controller superclass
    // wrapping these exceptions in a fatal ServletException
    //---------------------------------------------------------------------

    /**
     * Invalid input
     */
    public ModelAndView onInvalidInput(
            HttpServletRequest request,
            HttpServletResponse response,
            WebRequestBindingException ex) {
        return new ModelAndView(DEBUG_VIEW, "message", "Invalid input");
    }

} // class TicketController