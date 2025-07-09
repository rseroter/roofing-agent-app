package handlers

import (
	"fmt"
	"net/http"
	"strconv"

	"appointment-service/models"
	"appointment-service/store"

	"github.com/labstack/echo/v4"
)



// BookAppointmentHandler handles requests to book an appointment.
// POST /appointments/book
func (cfg *Config) BookAppointmentHandler(c echo.Context) error {
	var req models.BookingRequest
	if err := c.Bind(&req); err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "Invalid request body: " + err.Error()})
	}

	c.Logger().Infof("Incoming booking request: %+v", req)

	if req.SlotID == "" {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "Field 'slotId' is required"})
	}
	if req.CustomerName == "" {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "Field 'customerName' is required"})
	}
	if len(req.ServicesToBook) == 0 {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "Field 'servicesToBook' must not be empty"})
	}

	store.Mu.Lock()
	defer store.Mu.Unlock()

	slot, exists := store.AppointmentSlotsStore[req.SlotID]
	if !exists {
		return c.JSON(http.StatusNotFound, map[string]string{"error": fmt.Sprintf("Appointment slot with ID '%s' not found", req.SlotID)})
	}

	if slot.IsBooked {
		return c.JSON(http.StatusConflict, map[string]string{"error": fmt.Sprintf("Appointment slot ID '%s' is already booked", req.SlotID)})
	}

	// Validate if requested services are available in this slot
	validServices := make(map[string]bool)
	for _, srv := range slot.AvailableServices {
		validServices[srv] = true
	}
	for _, requestedSrv := range req.ServicesToBook {
		if !validServices[requestedSrv] {
			return c.JSON(http.StatusBadRequest, map[string]string{"error": fmt.Sprintf("Service '%s' is not available for slot ID '%s'", requestedSrv, req.SlotID)})
		}
	}

	// Mark appointment as booked
	slot.IsBooked = true
	slot.BookedBy = req.CustomerName
	slot.BookedServices = req.ServicesToBook

	// Generate a simple booking ID
	bookingID := "BOOK-" + strconv.Itoa(store.NextBookingID)
	store.NextBookingID++

	// Assign a roofing contact - simple round robin based on booking ID
	contactIndex := (store.NextBookingID - 1) % len(cfg.RoofingContacts) // -1 because nextBookingID was already incremented
	assignedContact := cfg.RoofingContacts[contactIndex]

	confirmation := models.BookingConfirmation{
		BookingID:      bookingID,
		RoofingContact: assignedContact,
		Message:        fmt.Sprintf("Appointment slot %s booked successfully for %s.", req.SlotID, req.CustomerName),
		SlotID:         slot.ID,
		Date:           slot.DateTime.Format("2006-01-02"),
		Time:           slot.DateTime.Format("03:04PM"),
		City:           slot.City,
		BookedServices: slot.BookedServices,
		CustomerName:   slot.BookedBy,
	}

	// Log the confirmation response before sending
	c.Logger().Infof("Booking successful. Confirmation details: %+v", confirmation)

	return c.JSON(http.StatusCreated, confirmation)
}