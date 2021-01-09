# Akka Prime number calculator (clustered)

To try this demo application out, you have to start a Master by running `me.steffenjacobs.akka.PrimeClusterApp.main` with port 1337 and the value `true`. The master will include two workers.

After this, you can start additional workers by running `me.steffenjacobs.akka.PrimeClusterApp.main` with a different port (e.g. 1338) and the value `false`.

This code is based on a blogpost from 2016 in *German* which can be found [here](https://blog.oio.de/2017/07/25/aktorenmodell-und-akka-teil-33).

## Troubleshooting
Make sure you use JDK 8 or compatibility mode (untested) when running this application. 