# Multiple Backstack + Navigation Extensions
This library provides navigation extensions to suport multiple backstack with BottomNavigationView. 

The extensions are build upon the [Advanced Navigation Sample](https://github.com/android/architecture-components-samples/blob/master/NavigationAdvancedSample/app/src/main/java/com/example/android/navigationadvancedsample/NavigationExtensions.kt).

## What problems are solved?
* Application will not restart on opening via deeplink.
* Logic for splash and login will be handled before opening deeplink.
* There are options to open a destination only once for both directions and uri. User will only navigate to a destination only once.


Most of the application needs to show a splash screen before navigating to their main screen. For this to work we have to create a fragment which will hold all the bottom navigation fragments. This fragment will on same level as of splash screen.

## How to use?
The library is best suited when you have single activity architecture. The primary activity must be using `singleTask` launch mode. 

```
android:launchMode="singleTask"
```

There are extension functions available to be used in activity and fragment.

### In you primary activity
1. `onHandleDeeplinkIntent` : call this method from activty's `onCreate` and `onNewIntent`.
2. If you are setting up your bottom navigation in fragment instead of activity you will have to call `setUpDeeplinkNavigationBehavior` before anything else.
3. If you bottom navigation is attached to activity itself you have to call `setupMultipleBackStackBottomNavigation`. This will complete the setup. This method is called in `onCreate` if `savedInstanceState` is null, otherwise this happens in `onRestoreInstanceState`.
```Kotlin
    if (savedInstanceState == null) {
        setUpBottomNavigation()
    }

    override fun onRestoreInstanceState(savedInstanceState:Bundle) 
    {
        super.onRestoreInstanceState(savedInstanceState)
        setUpBottomNavigation()
    }
```


### In you primary fragment
This is the fragment where bottom navigation is setup.

`setupMultipleBackStackBottomNavigation` call this method after meeting up with the activity requirements. This is all you need in fragment. Call this method once fragment state is restored.
```Kotlin
    override fun onViewStateRestored(savedInstanceState: Bundle?)
    {
        super.onViewStateRestored(savedInstanceState)
        setupBottomNavigationBar()
    }
```


## Available Extensions for multiple back stack navigation and deeplink
* `AppCompatActivity.setUpDeeplinkNavigationBehavior`: Setup the deeplink navigation behavior such as whether to exit the current flow and navigate or exit the flow. This also setups the necessary components required for navigation. This is only required when bottom navigation is present in a fragemnt instead of activity.

* `AppCompatActivity.handleDeeplinkIntent`: This will handle the intent for deeplink. Also mark `intent.data = null`. Take a function as parameter to perform tasks that are dependent on intent before setting `data` to null.

* `setupMultipleBackStackBottomNavigation`: This extension is available for both Activity and Fragment. This function does two things: 

    1. setup BottomNavigationView with multiple navHost to maintain back stack properly.
    2. Observe for any deeplink request for naviagtion via DeeplinkNavigator.navigate or via intent.

### Extensions for navigation
1. ``` fun NavController.navigateOnce(navigationRequest: NavigateOnceDeeplinkRequest) ```

    takes a deeplink request and perfrom navigation based on    arguments. The arguments can specify allowMultipleInstances and should update arguments or not.
    
    [NavigateOnceDeeplinkRequest](https://github.com/beetlestance/android-extensions/blob/main/navigation/src/main/java/com/beetlestance/androidextensions/navigation/NavigateOnceDeeplinkRequest.kt)

2. ```fun NavController.navigateOnce(navigationRequest: NavigateOnceDirectionRequest) ```

    takes a direction request and perfrom navigation based on    arguments. The arguments can specify allowMultipleInstances and should update arguments or not.
    
    [NavigateOnceDirectionRequest](https://github.com/beetlestance/android-extensions/blob/main/navigation/src/main/java/com/beetlestance/androidextensions/navigation/NavigateOnceDirectionRequest.kt) 

### Download


### What is the goal and future expectations?
The goal is to provide seemless navigation with all the available methods like Deeplink, Notifications, App links and simple destinations. 
Future Expectations:
1. Will showcase how FCM notifications can be handled via deeplnks and the above extensions
2. Provide youtube like backstack for bottom navigation
3. Handle App links via navigation components.

