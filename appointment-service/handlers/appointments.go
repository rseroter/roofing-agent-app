package handlers

import (
	"net/http"

	"appointment-service/models"
	"appointment-service/store"

	"github.com/labstack/echo/v4"
)

// GetAvailableAppointmentsHandler handles requests for available appointments by city.
// GET /appointments/available?city={city}
func GetAvailableAppointmentsHandler(c echo.Context) error {
	city := c.QueryParam("city")
	if city == "" {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "Query parameter 'city' is required"})
	}

	store.Mu.Lock() // Using a full lock for simplicity; RLock could be used if reads are much more frequent than writes.
	defer store.Mu.Unlock()

	var responseData []models.AvailableSlotResponseItem
	for _, apt := range store.AppointmentSlotsStore {
		if apt.City == city && !apt.IsBooked {
			responseData = append(responseData, models.AvailableSlotResponseItem{
				SlotID:   apt.ID,
				Date:     apt.DateTime.Format("2006-01-02"),
				Time:     apt.DateTime.Format("03:04PM"),
				Services: apt.AvailableServices,
				City:     apt.City,
			})
		}
	}
	//add log statement
	c.Logger().Infof("Found %d available slots for city '%s'", len(responseData), city)

	return c.JSON(http.StatusOK, responseData)
}