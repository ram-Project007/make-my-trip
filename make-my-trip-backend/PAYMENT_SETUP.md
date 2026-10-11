# Paytm Dynamic QR setup

The app creates one stable Paytm order ID per booking, reuses that order for QR retries, and confirms payment only after a signed server-side Transaction Status response matches the merchant, order ID, and booking amount. The browser never receives the merchant key.

Set these environment variables on the backend:

```text
PAYTM_MID=<merchant MID>
PAYTM_MERCHANT_KEY=<merchant key>
PAYTM_POS_ID=<enabled POS ID>
PAYTM_QR_URL=https://securegw.paytm.in/paymentservices/qr/create
PAYTM_STATUS_URL=https://securegw.paytm.in/v3/order/status
```

For staging, use the staging MID/key and set the QR URL to `https://securegw-stage.paytm.in/paymentservices/qr/create` and the status URL to `https://securegw-stage.paytmpayments.in/v3/order/status`.

The merchant account must have Paytm Dynamic QR enabled and UPI QR collection enabled. Keep the production merchant key in the hosting provider's secret manager or environment settings; do not commit it to this repository or expose it to the frontend.

The authenticated customer endpoints are `POST /booking/{bookingId}/payment/qr` and `POST /booking/{bookingId}/payment/verify`. The second endpoint queries Paytm from the backend; it does not trust a browser-provided success value or UTR. Repeated requests use the same booking order ID, and confirmation is an atomic pending-to-paid update.
