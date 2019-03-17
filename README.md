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
