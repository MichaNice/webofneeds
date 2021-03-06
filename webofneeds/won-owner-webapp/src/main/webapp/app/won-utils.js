/**
 * Created by ksinger on 11.08.2016.
 */

import Immutable from 'immutable';

export function initLeaflet(mapMount) {
    if(!L) {
        throw new Exception("Tried to initialize a leaflet widget while leaflet wasn't loaded.");
    }

    const baseMaps = initLeafletBaseMaps();

    const map = L.map(mapMount,{
        center: [37.44, -42.89], //centered on north-west africa
        zoom: 1, //world-map
        layers: [baseMaps['Detailed default map']], //initially visible layers

    }); //.setView([51.505, -0.09], 13);

    //map.fitWorld() // shows every continent twice :|
    map.fitBounds([[-80, -190],[80, 190]]); // fitWorld without repetition

    L.control.layers(baseMaps).addTo(map);

    // Force it to adapt to actual size
    // for some reason this doesn't happen by default
    // when the map is within a tag.
    // this.map.invalidateSize();
    // ^ doesn't work (needs to be done manually atm);

    return map;
}

export function initLeafletBaseMaps() {
    if(!L) {
        throw new Exception("Tried to initialize leaflet map-sources while leaflet wasn't loaded.");
    }
    const secureOsmSource = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png' // secure osm.org
    const secureOsm = L.tileLayer(secureOsmSource, {
        attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
    });

    const transportSource = 'http://{s}.tile2.opencyclemap.org/transport/{z}/{x}/{y}.png';
    const transport = L.tileLayer(transportSource, {
        attribution: 'Maps &copy; <a href="http://www.thunderforest.com">Thunderforest</a>, Data &copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap contributors</a>',
    });

    const baseMaps = {
        "Detailed default map": secureOsm,
        "Transport (Insecurely loaded!)": transport,
    };

    return baseMaps;
}

export function selectTimestamp(event, ownNeedUri) {
    /*
     * the "outer" event is from our own event
     * container. The receivedTimestamp there
     * should have been placed by our own node.
     *
     * The exception are events that haven't
     * been confirmed yet. They don't have the
     * timestamp.
     */
    return event.get('hasReceivedTimestamp');
};

export function selectConnectionUris(need) {
    return need
        .getIn(['won:hasConnections', 'rdfs:member'])
        .map(c => c.get('@id'));
}

export function selectEventsOfConnection(state, connectionUri) {
    const eventUris = state.getIn(['connections', connectionUri, 'hasEvents']);
    const eventUrisAndEvents = eventUris &&
        eventUris.map(eventUri => [
            eventUri,
            state.getIn(['events', 'events', eventUri])
        ]);
    return Immutable.Map(eventUrisAndEvents);
}
