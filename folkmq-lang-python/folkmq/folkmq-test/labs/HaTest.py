import asyncio

from socketd.utils.LogConfig import log

from folkmq.FolkMQ import FolkMQ
from folkmq.client.MqMessage import MqMessage


async def main():
    client = await (FolkMQ.createClient("folkmq:ws://127.0.0.1:18603?ak=ak1&sk=sk1",
                        "folkmq:ws://127.0.0.1:18703?ak=ak1&sk=sk1")
                    .name_as("demoapp")
                    .connect())

    # 订阅
    await client.subscribe("demo", None, None, lambda message:
        log.info(message.get_body_as_string())
    )

    i:int = 0
    # 发布
    while True:
        i+=1
        client.publish_async("demo", MqMessage("test-" + str(i)))

    await asyncio.Future()

if __name__ == "__main__":
    asyncio.run(main())

