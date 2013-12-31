var raster = new ol.layer.Tile({
  source: new ol.source.MapQuestOSM()
});

var style = new ol.style.Style({
  symbolizers: [
    new ol.style.Icon({
      url: 'cake.png',
      yOffset: -22
    })
  ]
});

var vector = new ol.layer.Vector({
  source: new ol.source.Vector({
    features: [
      new ol.Feature({
        name: 'Null Island',
        population: 4000,
        rainfall: 500,
        geometry: new ol.geom.Point([0, 0])
      })
    ]
  }),
  style: style
});

var map = new ol.Map({
  layers: [raster],
  renderer: ol.RendererHint.CANVAS,
  target: 'content',
  view: new ol.View2D({
    center: [0, 0],
    zoom: 3
  })
});
