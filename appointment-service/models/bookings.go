package models

// BookingRequest is the structure for a new roofing service booking.
type BookingRequest struct {
	SlotID         string   `json:"slotId"`
	CustomerName   string   `json:"customerName"`
	FullAddress    string   `json:"fullAddress"`
	ServicesToBook []string `json:"servicesToBook"`
}

// BookingConfirmation is the response after a successful booking.
type BookingConfirmation struct {
	BookingID      string   `json:"bookingId"`
	RoofingContact string   `json:"roofingContact"`
	Message        string   `json:"message"`
	SlotID         string   `json:"slotId"`
	Date           string   `json:"date"`
	Time           string   `json:"time"`
	City           string   `json:"city"`
	BookedServices []string `json:"bookedServices"`
	CustomerName   string   `json:"customerName"`
}