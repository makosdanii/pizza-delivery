const token = ""
const url = "https://api.mapbox.com/datasets/v1/makosdanii/clgapu42b0wf32bpdpwzjvwwu/features?access_token=" + token
const url2 = "https://api.mapbox.com/datasets/v1/makosdanii/clgb4eeme00ka2oqkh6n0mq1i/features?access_token=" + token
const axios = require('axios');


const normalizeStreets = (arr) => {
    let streetNames = [];
    let foreignKeyCol = [];
    let idx = -1;
    for (const {adj, edge_name, edge_weight, id, until} of arr) {
        idx = streetNames.findIndex(obj => obj.edge_name === edge_name);
        if (idx === -1) {
            //API may provide declared value for until
            streetNames.push({edge_name, until: until == null ? Math.trunc(Math.random() * 100) : until})

            foreignKeyCol.push(streetNames.length)
        } else {
            foreignKeyCol.push(idx + 1)
        }
    }

    arr = arr.map((properties, idx) => Object.assign(properties, {edge_name: foreignKeyCol[idx]}))

    return streetNames
}

const insertIntoStreetName = (arr) => {
    console.log("INSERT INTO street_name(that, until_no) values")

    for (const {edge_name, until} of arr.slice(0, arr.length - 1)) {
        console.log(`('${edge_name}', ${until}),`)
    }

    const {edge_name, until} = arr[arr.length - 1]
    console.log(`('${edge_name}', ${until});`)
}

const insertIntoEdge = (arr, streetNames) => {
    console.log("INSERT INTO edge(id, vertex, edge_name, edge_weight) values")

    //if API provides multiple edges for a street then it provides until_no. for each as well 
    for (const {adj, edge_name, edge_weight, id, until} of arr.slice(0, arr.length - 1)) {
        console.log(`(${id}, ${until == null ? streetNames[edge_name - 1].until : until}, ${edge_name}, ${edge_weight}),`)
    }

    const {adj, edge_name, edge_weight, id, until} = arr[arr.length - 1]
    console.log(`(${id}, ${until == null ? streetNames[edge_name - 1].until : until}, ${edge_name}, ${edge_weight});`)
}

const insertIntoMap = (arr) => {
    console.log("INSERT INTO map values")

    for (const {adj, edge_name, edge_weight, id, until} of arr.slice(0, arr.length - 1)) {
        if (adj instanceof Number) {
            console.log(`(${id}, ${adj}),`)
        } else {
            for (fk of adj.split(',')) {
                console.log(`(${id}, ${fk.trim()}),`)
            }
        }
    }

    let {adj, edge_name, edge_weight, id, until} = arr[arr.length - 1]
    let adjs = adj.split(',');
    for (const fk of adjs.slice(0, adjs.length - 1)) {
        console.log(`(${id}, ${fk.trim()}),`)
    }
    console.log(`(${id}, ${adjs[adjs.length - 1].trim()});`)
}

axios.get(url2).then(resp2 => {
    axios.get(url).then(resp => {
        const data2 = Object.values(resp2.data.features).map(obj => obj.properties);
        let data = data2.concat(Object.values(resp.data.features).map(obj => obj.properties));
        data = data.sort((a, b) => a.id - b.id)

        const streetNames = normalizeStreets(data)
        // console.log(streetNames)
        // console.log(data)
        insertIntoStreetName(streetNames)
        insertIntoEdge(data, streetNames)
        insertIntoMap(data)
    });
});