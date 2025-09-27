# Rest API Design

## Controllers

### Hub Controller

#### hubReading - POST

Stores reading in DB

```json
{
    "apiKey": 123456
    "probes": [
        {"probeId": 1, "currentTemp": 20.00},
        {"probe": 2, "currentTemp": 50.00},
        {"probe": 3, "currentTemp": 23.00},
        {"probe": 4, "currentTemp": 50.00},
    ]
}
```

returns 200

#### CurrentCook - GET

Returns the target temps for each probe

```json
{
    "apiKey": 123456,
    "hubName": "Smoker",
    "probes": [
        {"probe": 1, "targetTemp": 20.00},
        {"probe": 2, "targetTemp": 50.00},
        {"probe": 3, "targetTemp": 23.00},
        {"probe": 4, "targetTemp": 50.00},
    ]
}
```
