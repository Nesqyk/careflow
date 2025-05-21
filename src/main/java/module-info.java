module edu.careflow {
    // Required modules
    requires javafx.fxml;
    requires java.sql;
    requires com.dlsc.gemsfx;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires opencv;
    requires javafx.web;


    // Exports packages from the correct paths
//    exports edu.careflow.application;
//    exports edu.careflow.application.config;
//    exports edu.careflow.presentation;


    exports  edu.careflow.presentation.controllers.admin;
    opens edu.careflow.presentation.controllers.admin;
    opens edu.careflow.presentation.controllers.receptionist;
    exports edu.careflow.presentation.controllers.receptionist;
    exports edu.careflow.presentation.controllers.nurse;
    exports edu.careflow.presentation.controllers.patient.forms;
    requires org.apache.poi.ooxml;
    requires java.desktop;
    requires ical4j.core;
    exports  edu.careflow.presentation.controllers.doctor;
    exports  edu.careflow.presentation.controllers.doctor.cards;
    exports edu.careflow.manager;
    exports edu.careflow.repository.entities;
    exports edu.careflow.repository.dao;
    exports edu.careflow.presentation.controllers;
    exports edu.careflow.presentation.controllers.patient.cards;
    exports edu.careflow to javafx.graphics;
//    exports edu.careflow.presentation.views;
//    exports edu.careflow.presentation.viewmodels;
//    exports edu.careflow.presentation.components;
//    exports edu.careflow.service;
//    exports edu.careflow.service.patient;
//    exports edu.careflow.service.appointment;
//    exports edu.careflow.service.prescription;
//    exports edu.careflow.service.billing;
//    exports edu.careflow.service.reporting;
//    exports edu.careflow.service.security;
//    exports edu.careflow.service.integration;
//    exports edu.careflow.repository;
//    exports edu.careflow.repository.dao;
//    exports edu.careflow.repository.entities;
//    exports edu.careflow.repository.mappers;
//    exports edu.careflow.repository.cache;
    exports edu.careflow.utils;
//    exports edu.careflow.util.logging;
//    exports edu.careflow.util.validation;
//    exports edu.careflow.util.exception;

    // Opens packages to allow reflective access for JavaFX
    exports edu.careflow.presentation.controllers.components.table to javafx.fxml;
    exports  edu.careflow.presentation.controllers.components.user to javafx.fxml;

    opens edu.careflow.presentation.controllers.components.user to javafx.fxml;

    opens edu.careflow.presentation.controllers.nurse;
    opens edu.careflow.presentation.controllers.components.table;
    opens edu.careflow.presentation.controllers.patient.forms;
    opens edu.careflow.presentation.controllers to javafx.fxml;
    opens edu.careflow.presentation.controllers.patient.cards to javafx.fxml;
    opens edu.careflow.presentation.controllers.doctor;
    opens edu.careflow.presentation.controllers.doctor.cards;
    exports edu.careflow.presentation.controllers.patient;
    opens edu.careflow.presentation.controllers.patient to javafx.fxml;
//    opens edu.careflow.presentation.components.states to javafx.fxml;
//    opens edu.careflow.presentation.components to javafx.fxml;
//    opens edu.careflow.presentation.views to javafx.fxml;
//    opens edu.careflow.repository.dao to javafx.base;
}
