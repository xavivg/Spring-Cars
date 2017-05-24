package com.mycompany.myapp.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.mycompany.myapp.domain.Car;

import com.mycompany.myapp.domain.Manufacturer;
import com.mycompany.myapp.domain.enumeration.Segment;
import com.mycompany.myapp.repository.CarByCriteriaRepository;
import com.mycompany.myapp.repository.CarRepository;
import com.mycompany.myapp.web.rest.util.HeaderUtil;
import com.mycompany.myapp.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for managing Car.
 */
@RestController
@RequestMapping("/api")
public class CarResource {

    private final Logger log = LoggerFactory.getLogger(CarResource.class);

    @Inject
    private CarRepository carRepository;
    @Inject
    private CarByCriteriaRepository carByCriteriaRepository;

    /**
     * POST  /cars : Create a new car.
     *
     * @param car the car to create
     * @return the ResponseEntity with status 201 (Created) and with body the new car, or with status 400 (Bad Request) if the car has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/cars")
    @Timed
    public ResponseEntity<Car> createCar(@RequestBody Car car) throws URISyntaxException {
        log.debug("REST request to save Car : {}", car);
        if (car.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("car", "idexists", "A new car cannot already have an ID")).body(null);
        }
        Car result = carRepository.save(car);
        return ResponseEntity.created(new URI("/api/cars/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("car", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /cars : Updates an existing car.
     *
     * @param car the car to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated car,
     * or with status 400 (Bad Request) if the car is not valid,
     * or with status 500 (Internal Server Error) if the car couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/cars")
    @Timed
    public ResponseEntity<Car> updateCar(@RequestBody Car car) throws URISyntaxException {
        log.debug("REST request to update Car : {}", car);
        if (car.getId() == null) {
            return createCar(car);
        }
        Car result = carRepository.save(car);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("car", car.getId().toString()))
            .body(result);
    }

    /**
     * GET  /cars : get all the cars.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of cars in body
     */
    @GetMapping("/cars")
    @Timed
    public List<Car> getAllCars() {
        log.debug("REST request to get all Cars");
        List<Car> cars = carRepository.findAllWithEagerRelationships();
        return cars;
    }

    /**
     * GET  /cars/:id : get the "id" car.
     *
     * @param id the id of the car to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the car, or with status 404 (Not Found)
     */
    @GetMapping("/cars/{id}")
    @Timed
    public ResponseEntity<Car> getCar(@PathVariable Long id) {
        log.debug("REST request to get Car : {}", id);
        Car car = carRepository.findOneWithEagerRelationships(id);
        return Optional.ofNullable(car)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /cars/:id : delete the "id" car.
     *
     * @param id the id of the car to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/cars/{id}")
    @Timed
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        log.debug("REST request to delete Car : {}", id);
        carRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("car", id.toString())).build();
    }

    @RequestMapping(value = "/car/byfilters",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional

        public ResponseEntity<List<Car>> getCarsByFilters(
        Pageable pageable,
        @RequestParam(value = "sales", required = false) String sales,
        @RequestParam(value = "minPrice", required = false) String minPrice,
        @RequestParam(value = "maxPrice", required = false) String maxPrice,
        @RequestParam(value = "model", required = false) String model,
        @RequestParam(value = "segment", required = false) Segment segment,
        @RequestParam(value = "manufacturer", required = false) String manufacturer
        ) throws URISyntaxException {

        Map<String, Object> params = new HashMap<>();

        if (sales != null) {

            try {
                Integer salesInt = Integer.parseInt(sales);
                params.put("sales", salesInt);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("car",
                    "number format exception on param",
                    "A numeric param cannot have non numeric characters")).body(null);
            }

        }

        if (minPrice != null) {

            try {
                Double minPriceDouble = Double.parseDouble(minPrice);
                params.put("minPrice", minPriceDouble);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("car",
                    "number format exception on param",
                    "A numeric param cannot have non numeric characters")).body(null);
            }

        }

        if (maxPrice != null) {

            try {
                Double maxPriceDouble  = Double.parseDouble(maxPrice);
                params.put("maxPrice", maxPriceDouble);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("car",
                    "number format exception on param",
                    "A numeric param cannot have non numeric characters")).body(null);
            }

        }

        if (model != null) { //TODO:Custom error If not string

            try {
                params.put("model", model);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("car",
                    "number format exception on param",
                    "A numeric param cannot have non numeric characters")).body(null);
            }

        }

        if (segment != null) { //TODO:Custom error If not string

            try {
                params.put("segment", segment);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("car",
                    "number format exception on param",
                    "A numeric param cannot have non numeric characters")).body(null);
            }

        }

        if (manufacturer != null) { //TODO:Custom error If not a manufacturer obj

            try {
                params.put("manufacturer", manufacturer);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("car",
                    "number format exception on param",
                    "A numeric param cannot have non numeric characters")).body(null);
            }

        }


        List<Car> result = carByCriteriaRepository.filteryCarByCriteria(params,pageable);

            Page<Car> page = new PageImpl<Car>(result, pageable, carRepository.findAll().size());
            HttpHeaders httpHeaders = new HttpHeaders();

            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/car/byfilters");
            return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);


        }
    }


