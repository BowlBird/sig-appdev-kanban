# sig-appdev-kanban

An example project to check your work!
What I want from you

Only use this repo if you are stuck! It's best to learn by just trying to get through this.

I want you to make a tip calculator app that

- Supports 3 Categories, TODO, DOING, and DONE
- You can add to any of the categories, delete any entry, or move entries to different categories
- the 'Add' Composable should be on a different screen than the main contents (NavHost)
- Use Full MVVM architecture!
  - Datasources for interacting with different apis
  - Repositories to abstract Datasources
  - ViewModels to hold state data and update repositories
    - ViewModels should implement the companion object Factory method
  - Jetpack Compose Composables
- Use a Room database to persist state
- One extra fun feature!
