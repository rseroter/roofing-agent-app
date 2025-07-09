package models

import "time"

// AppointmentSlot represents an available or booked time slot for roofing services.
type AppointmentSlot struct {
	ID                string    `json:"id"`                // Internal ID for the slot
	DateTime          time.Time `json:"-"`                 // Internal, not directly exposed in GET list
	City              string    `json:"city"`              // City where the service is offered
	AvailableServices []string  `json:"availableServices"` // Services offered in this slot
	IsBooked          bool      `json:"isBooked"`
	BookedBy          string    `json:"bookedBy,omitempty"`       // Customer name
	BookedServices    []string  `json:"bookedServices,omitempty"` // Services chosen by customer
}

// AvailableSlotResponseItem is the structure for displaying an available slot.
type AvailableSlotResponseItem struct {
	SlotID   string   `json:"slotId"`
	Date     string   `json:"date"`
	Time     string   `json:"time"`
	Services []string `json:"services"`
	City     string   `json:"city"`
}