# Spent
Categorise payments for budgeting and spend tracking.

## What it does?

**Spent** will listen for notifications from payments on your 
Android device using services like Android Pay or PayPal. Each 
notification will be captured and categorised. This will allow the 
following:

* Know exactly how much you're spending on Coffee (Groceries, 
movies...).
* Capture transaction information without needing to scan receipts.
* Budget effectively.

## What apps are supported?

The following applications/notifications are supported:
* Android Pay. 
  * Notifications must be enabled in Android Pay and Google Play 
  Services (particularly the Other Notifications channel for Android 
  8+).
* PayPal. 
  * Push Notifications after a payment must be enabled in the PayPal
  application.

### Adding support for new applications.

New applications can be supported by adding a function to the 
`TransactionNotificationListener` service. A notification from the
desired application should be captured for analysis of the content
so transaction information can be reliably parsed.

**Note** this can break if an application changes the format of 
their notifications.

## How are transactions categorised?

Transactions are initially categorised as `Unknown`. Users can edit
transactions to select one of the default categories or add a custom
category that better suits the transaction.

The application will attempt to automatically categorise transactions
based upon previous transactions to the same merchant (can be 
disabled in the settings).

## Google Maps API key.

People wanting to fork and build their own application will need to 
register for a Google Maps API key following Google's guides.

This key is expected to be stored in `gradle.properties` as follows:

```yaml
GOOGLE_MAPS_API_KEY=THIS_IS_THE_SECRET_KEY
```

This is used by `build.gradle` at the app level when the application 
is built. 

## Disclaimer

This application **CANNOT** make transactions and only captures the 
content of the notifications specified in **What apps are supported?**.

No guarantee is provided that transactions recorded in app will 
match the transactions recorded in the statement from your financial
institution.

Some merchant names in app may appear different from those on your
statement.

All handling of notification content is done in the 
`TransactionNotificationListener` class.

**No** information is sent to third party servers. Location data can
be disabled if you don't wish to record the location of transactions.
Disabling location will not stop transactions being saved.

## Contact.

Please create issues on this repository.
