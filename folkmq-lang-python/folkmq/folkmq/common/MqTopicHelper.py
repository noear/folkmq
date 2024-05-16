class MqTopicHelper:
    #获取完整主题
    @staticmethod
    def get_full_topic(namespace:str, topic:str):
        if namespace:
            return f"{namespace}:{topic}"
        else:
            return topic

    #获取主题
    @staticmethod
    def get_topic(fullTopic:str):
        idx = fullTopic.find(":")
        if  idx > 0:
            return fullTopic[idx + 1]
        else:
            return fullTopic