# BFST23Group 17

`TODO: Fill out README.md`

---
### Members
- Marcus Aandahl (`maraa`)
- Villads Grum (`vilg`)
- Thomas Rand (`tdra`)
- Andreas Bartholdy (`anbc`)
- Christian Laustsen (`chrla`)

### JSON How-To
- Bounds:
bounds for drawn objects are calculated in MapView, line 207.
These determine how far in you have to zoom to see different layers in detail (the lower the bound number, the more you have to zoom in)
- Layers: Determines how the map is drawn in terms of what goes on top of what.
BE CAREFUL when changing these, and test often.
- Priority: Determines which tag has higher priority when drawing. If something is both one tag and another, the tag with the lowest priority is chosen to be drawn.