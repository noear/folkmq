const {FolkMQ} = require("@noear/folkmq");

async function main() {
    const client =  await FolkMQ.createClient("folkmq:ws://127.0.0.1:18603?ak=ak1&sk=sk1",
        "folkmq:ws://127.0.0.1:18703?ak=ak1&sk=sk1")
        .nameAs("demoapp")
        .connect();

    //订阅
    client.subscribe("demo", null, true, message => {
        console.log(message.getBodyAsString());
    });

    //发布
    let i=0;
    while (true) {
        i++
        client.publish("demo", FolkMQ.newMessage("hot-" + i));
    }
}

main();