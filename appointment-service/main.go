package main

import (
	"appointment-service/handlers"
	"appointment-service/store"

	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"
	"github.com/labstack/gommon/log"
)

func main() {
	store.InitializeAppointmentSlots()

	e := echo.New()

	// Middleware
	e.Use(middleware.Logger())
	e.Use(middleware.Recover())

	e.Logger.SetLevel(log.INFO)

	// Endpoint to get available appointments by city
	// GET /appointments/available?city={city}
	e.GET("/appointments/available", handlers.GetAvailableAppointmentsHandler)

	cfg := &handlers.Config{
		RoofingContacts: roofingContacts,
	}

	// Endpoint to book an appointment
	// POST /appointments/book
	e.POST("/appointments/book", cfg.BookAppointmentHandler)

	port := "8080"
	e.Logger.Infof("Starting roofing appointment-service on port %s", port)
	e.Logger.Infof("Available endpoints:")
	e.Logger.Infof("  GET  /appointments/available?city=<city_name>")
	e.Logger.Infof("  POST /appointments/book (Body: {\"slotId\": \"<id>\", \"customerName\": \"<name>\", \"servicesToBook\": [\"service1\", \"service2\"]})")
	e.Logger.Infof("Example GET: curl \"http://localhost:%s/appointments/available?city=New%%20York\"", port)
	e.Logger.Infof("Example POST: curl -X POST -H \"Content-Type: application/json\" -d '{\"slotId\": \"slot1\", \"customerName\": \"John Doe\", \"servicesToBook\": [\"roof inspection\"]}' http://localhost:%s/appointments/book", port)

	// Start server
	e.Logger.Fatal(e.Start(":" + port))
}