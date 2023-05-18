<script>
import mapboxgl from 'mapbox-gl'

export default {
  data() {
    return {
      name: "Map",
      token: "pk.eyJ1IjoibWFrb3NkYW5paSIsImEiOiJjbGdhb21hNHQxMndwM2xwaWUxOXY0bGU5In0.QCQaAMl0L4JC4gkOyTBzAg",
      url: 'mapbox://styles/makosdanii/clgaouuen000901n16y2jhfwz'
    }
  },
  mounted() {
    mapboxgl.accessToken = this.token
    const map = new mapboxgl.Map({
      container: 'map',
      style: this.url,
      center: [19.047, 47.493],
      zoom: 12.85,
      pitch: 70.0,
      bearing: 15,
    })

    map.on('load', () => {
      map.loadImage(
          'src/assets/marker.png',
          (error, image) => {
            if (error) throw error;
            map.addImage('pizza', image);
// Add a data source containing one point feature.
            map.addSource('point', {
              'type': 'geojson',
              'data': {
                'type': 'FeatureCollection',
                'features': [
                  {
                    'type': 'Feature',
                    'geometry': {
                      'type': 'Point',
                      'coordinates': [19.0535, 47.4762]
                    }
                  }
                ]
              }
            });

// Add a layer to use the image to represent the data.
            map.addLayer({
              'id': 'points',
              'type': 'symbol',
              'source': 'point', // reference the data source
              'layout': {
                'icon-image': 'pizza', // reference the image
                'icon-size': 0.5,

              }
            });
          }
      );
    });
  }
}
</script>

<template>
  <div id="map"></div>
</template>

<style>
@import 'mapbox-gl/dist/mapbox-gl.css';

.mapboxgl-canvas-container {
  height: 600px;
  width: 1200px;
}
</style>