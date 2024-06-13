import asyncio

from socketd.utils.LogConfig import log

from folkmq.FolkMQ import FolkMQ
from folkmq.client.MqMessage import MqMessage


async def main():
    client = (FolkMQ.create_client("folkmq:ws://127.0.0.1:18602")
                    .name_as("demoapp"))

    # 订阅
    await client.subscribe("test", None, True, lambda message:
        log.info(message.get_body_as_string())
    )

    await client.subscribe("test2", None, True, lambda message:
        log.info(message.get_body_as_string())
    )

    await client.subscribe("test3", None, True, lambda message:
        log.info(message.get_body_as_string())
    )

    # 连接
    await client.connect()



    # 发布
    for i in range(10):
        await client.publish("test", MqMessage("test-" + str(i)))
        await client.publish("test2", MqMessage("test2-" + str(i)))

    await asyncio.Future()

if __name__ == "__main__":
    asyncio.run(main())

