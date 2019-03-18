### Requirements

Create a git repository (either local or public one on GitHub) that contains a RESTful web-service written in Scala. The service should allow users to place new car adverts and view, modify and delete existing car adverts.

Car adverts should have the following fields:
* **id** (_required_): **int** or **guid**, choose whatever is more convenient for you;
* **title** (_required_): **string**, e.g. _"Audi A4 Avant"_;
* **fuel** (_required_): gasoline or diesel, use some type which could be extended in the future by adding additional fuel types;
* **price** (_required_): **integer**;
* **new** (_required_): **boolean**, indicates if car is new or used;
* **mileage** (_only for used cars_): **integer**;
* **first registration** (_only for used cars_): **date** without time.

Service should:
* have functionality to return list of all car adverts;
  * optional sorting by any field specified by query parameter, default sorting - by **id**;
* have functionality to return data for single car advert by id;
* have functionality to add car advert;
* have functionality to modify car advert by id;
* have functionality to delete car advert by id;
* have validation (see required fields and fields only for used cars);
* accept and return data in JSON format, use standard JSON date format for the **first registration** field.

### Additional requirements

* Service should be able to handle CORS requests from any domain.
* Think about test pyramid and write unit-, integration- and acceptance-tests if needed.
* It's not necessary to document your code, but having a readme.md for developers who will potentially use your service would be great.


### Motivation for client-generated IDs

A car advert doesn't have a natural ID, so it has to be an artifical one.
There are basically two options:
- Server-generated ID (for example, using auto-increment id field in the database)
- Client-generated unique ID (UUID is an easy way)

So if the client doesn't have an ID before receiving a response from the service,
we have a well-known problem in distributed systems caused by unreliable network.
It might be the case that the transaction is commited on the service side,
but due to a network failure this information is lost on the way back to the client.
So as the client doesn't know if his request was successful or not (and there is
no entry ID to check that), it has to retry, thus creating a duplicate entry.

But if an ID is assigned on the client side, duplicate requests might be identified
and properly processed on the server side.

Therefore, the design decision is taken here to require client-generated IDs (UUIDs)
for creation of new car adverts.


### Further improvements

Well, that's already quite a lot of code. Due to limited time some nice-to-haves are left out:

- Clarify if all updates are deemed valid. For example updating 'new' field from false to true sounds like a fraud :)
- Add some logging
- Use a real database for persistence layer
- Unit testing of sorting with different fields
- Document API (e.g. with Swagger)
- Use "RFC 7807 - Problem Details for HTTP APIs" to describe errors