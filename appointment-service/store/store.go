package store

import (
	"sync"
	"time"

	"appointment-service/models"
)

var (
	AppointmentSlotsStore = make(map[string]*models.AppointmentSlot)
	// Mu to protect concurrent access to appointmentsStore.
	Mu sync.Mutex
	// NextBookingID is a simple counter for generating booking IDs.
	NextBookingID = 1
)

// InitializeAppointmentSlots populates the store with some sample data.
// All services used for AvailableServices and BookedServices in this function
// are chosen from the AllowedRoofingServices list.
func InitializeAppointmentSlots() {
	now := time.Now()
	AppointmentSlotsStore["slot1"] = &models.AppointmentSlot{
		ID: "slot1", DateTime: time.Date(now.Year(), now.Month(), now.Day()+1, 9, 0, 0, 0, time.Local),
		City: "San Diego", AvailableServices: []string{"roof inspection", "gutter cleaning"}, IsBooked: false,
	}
	AppointmentSlotsStore["slot2"] = &models.AppointmentSlot{
		ID: "slot2", DateTime: time.Date(now.Year(), now.Month(), now.Day()+1, 13, 0, 0, 0, time.Local),
		City: "San Diego", AvailableServices: []string{"roof repair"}, IsBooked: false,
	}
	AppointmentSlotsStore["slot3"] = &models.AppointmentSlot{
		ID: "slot3", DateTime: time.Date(now.Year(), now.Month(), now.Day()+2, 11, 0, 0, 0, time.Local),
		City: "Stockholm", AvailableServices: []string{"skylight installation", "roof replacement"}, IsBooked: false,
	}
	AppointmentSlotsStore["slot4"] = &models.AppointmentSlot{
		ID: "slot4", DateTime: time.Date(now.Year(), now.Month(), now.Day()+2, 14, 0, 0, 0, time.Local),
		City: "Stockholm", AvailableServices: []string{"roof inspection", "roof repair"}, IsBooked: false,
	}
	AppointmentSlotsStore["slot5"] = &models.AppointmentSlot{
		ID: "slot5", DateTime: time.Date(now.Year(), now.Month(), now.Day()+3, 10, 0, 0, 0, time.Local),
		City: "San Diego", AvailableServices: []string{"gutter cleaning", "roof inspection"}, IsBooked: false,
	}
	AppointmentSlotsStore["slot6"] = &models.AppointmentSlot{
		ID: "slot6", DateTime: time.Date(now.Year(), now.Month(), now.Day()+3, 16, 0, 0, 0, time.Local),
		City: "San Diego", AvailableServices: []string{"gutter cleaning", "roof repair", "roof replacement"}, IsBooked: false,
	}
}