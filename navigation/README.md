# Multiple Backstack + Navigation Extensions
This library provides navigation extensions to suport multiple backstack with BottomNavigationView. 

The extensions are build upon the [Advanced Navigation Sample](https://github.com/android/architecture-components-samples/blob/master/NavigationAdvancedSample/app/src/main/java/com/example/android/navigationadvancedsample/NavigationExtensions.kt).

### What problems are solved?
* Application will not restart on opening via deeplink.
* Logic for splash and login will be handled before opening deelink.


Most of the application needs to show a splash screen before navigating to their main screen. For this to work we have to create a fragment container which will hold all the bottom navigation fragments. This fragment container will on same level as of splash screen.


### How to use?
There are few steps to follow here:

1. Set your launcher activity's launch mode as `singleTop` . This will redirect all the deeplink launch intents to activity if it already open.
```
android:launchMode="singleTop"
```

2. Now we need to get the deeplink for `intent.data`. This can be received in `onCreate` or `onHandleNewIntent`. Pass this deeplink to a singleton class as in our sample we have used singleton injected class [TopLevelNavigatorDelegate](https://github.com/beetlestance/android-extensions/blob/main/sample/src/main/java/com/beetlestance/androidextensions/sample/TopLevelNavigatorViewModelDelegate.kt). There are two methods in this class one to pass deeplink when app was not running and one when app is running and pass the deeplink to a livedata which will be observed in fragment containing bottom navigation. 

    The most crucial part here is to set the `intent.data = null` to prevent any misbehave.
    See the sample [MainActivity](https://github.com/beetlestance/android-extensions/blob/main/sample/src/main/java/com/beetlestance/androidextensions/sample/MainActivity.kt).


    We handle the deeplink navigation in the fragment which contains bottom navigation. So that splash and other screen will display first. 

3. Now we have to set up our fragment which contains bottom navigation.

    a. We have to setup bottom navigation in onViewrestored
    ```Kotlin
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        //setupBottomNavigationBar()
    }
    ``` 

    b. Before setting up bototm navigation we check if there is any deeplink to handle as extention method setupWithNavController will take this deeplink. This is deeplink is stored in a singleton class early in MainActivity.

    ```Kotlin
    private fun setupBottomNavigationBar() {
        // Create a request for extension
        var deepLink: NavigateOnceDeeplinkRequest? = null
        viewModel.handleDeeplinkIfAny?.let {
            // validate deeplink, like if user is not logged in 
            //  move to diferent deeplink
            // write your own implementation here
            handleDeeplinkOrElse(it) { validDeeplink ->
                deepLink = validDeeplink
            }
            viewModel.handleDeeplinkIfAny = null
        }

        // Setup the bottom navigation view with a list of
        // navigation graphs
        requireBinding().dashboardFragmentBottomNavigation.setupWithNavController(
            navGraphIds = NAV_GRAPH_IDS,
            fragmentManager = childFragmentManager,
            containerId = R.id.nav_host_fragment_dashboard,
            request = deepLink
        ).observe(viewLifecycleOwner) { navController ->
            currentNavController = navController

            // Choose when to show/hide the Bottom Navigation View
            navController.addOnDestinationChangedListener { _, destination, _ ->

                when {
                    TOP_LEVEL_DESTINATION.contains(destination.id) -> {
                        showNavigators()
                    }
                    else -> {
                        hideNavigators()
                    }
                }
            }
        }
    }

    ```


    c. Observe for new intent via method in toplevelnavigator or your own singleton class.
    ```Kotlin
    viewModel.navigatorDeeplink.observeEvent(viewLifecycleOwner) {
        // validate deeplink and then pass the request
            handleDeeplinkOrElse(it) { deeplink -> 
            bottomNavigationHandleDeeplink(deeplink) }
        }


    private fun bottomNavigationHandleDeeplink(request: NavigateOnceDeeplinkRequest) {
        requireBinding().dashboardFragmentBottomNavigation.post {
            // extension function in deeplink
            requireBinding().dashboardFragmentBottomNavigation.navigateDeeplink(
                navGraphIds = NAV_GRAPH_IDS,
                fragmentManager = childFragmentManager,
                containerId = R.id.nav_host_fragment_dashboard,
                request = request
            )
        }
    }
    ```

### For clearing backstack before navigating
We can use another method in toplevelnavigator to set the live data to true and observe in fragment which needs top pop before navigating.
```Kotlin
viewModel.clearBackStack.observeEvent(viewLifecycleOwner) {
            if (it) findNavController().popBackStack(R.id.dashboardFragment, false)
        }
```

### Download




